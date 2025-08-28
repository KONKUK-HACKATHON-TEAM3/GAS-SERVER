package com.gas.server.global.s3;

import com.gas.server.global.exception.BusinessException;
import com.gas.server.global.exception.ErrorType;
import java.io.IOException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Slf4j
@Service
@RequiredArgsConstructor
public class S3Service {

    private final S3Client s3Client;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    @Value("${cloud.aws.region.static}")
    private String region;

    public String uploadFile(MultipartFile multipartFile) {
        String originalFilename = multipartFile.getOriginalFilename();
        String fileExtension = getFileExtension(originalFilename);
        String fileName = generateFileName(fileExtension);

        try {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(fileName)
                    .contentType(multipartFile.getContentType())
                    .contentLength(multipartFile.getSize())
                    .build();

            s3Client.putObject(putObjectRequest,
                    RequestBody.fromInputStream(multipartFile.getInputStream(), multipartFile.getSize()));

            log.info("Successfully uploaded file to S3: {}", fileName);
        } catch (IOException e) {
            log.error("Failed to upload file to S3", e);
            throw new BusinessException(ErrorType.INTERNAL_SERVER_ERROR);
        }

        // S3 직접 URL 반환
        return String.format("https://%s.s3.%s.amazonaws.com/%s",
                bucket, region, fileName);
    }

    private String generateFileName(String fileExtension) {
        return UUID.randomUUID() + "." + fileExtension;
    }

    private String getFileExtension(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            throw new BusinessException(ErrorType.INVALID_REQUEST_BODY_ERROR);
        }
        
        return fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
    }

    public boolean isImageFile(MultipartFile file) {
        String contentType = file.getContentType();

        return contentType != null && contentType.startsWith("image/");
    }
}
