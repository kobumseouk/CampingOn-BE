package site.campingon.campingon.common.s3bucket.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


@Service
@RequiredArgsConstructor
public class S3BucketService {
  private final AmazonS3 s3Client; // S3 클라이언트 주입

  @Value("${aws.s3.bucket.name}")
  private String bucketName;

  // 단일 파일 업로드
  public String upload(MultipartFile file, String path) throws IOException {
    String fileName = generateValidPath(path) + createUniqueFileName(file);
    String fileUrl = "https://" + bucketName + ".s3.amazonaws.com/" + fileName;

    try {
      s3Client.putObject(bucketName, fileName, file.getInputStream(), createFileMetadata(file));
      return fileUrl;
    } catch (IOException e) {
      throw new IOException("파일 업로드 실패: " + e.getMessage());
    }
  }

  // 다중 파일 업로드
  public List<String> upload(List<MultipartFile> files, String path) throws IOException {
    List<String> uploadedUrls = new ArrayList<>();
    List<String> failedFiles = new ArrayList<>();

    for (MultipartFile file : files) {
      try {
        uploadedUrls.add(upload(file, path));
      } catch (IOException e) {
        failedFiles.add(file.getOriginalFilename());
      }
    }

    if (!failedFiles.isEmpty()) {
      throw new IOException("다음 파일들의 업로드가 실패했습니다: " + String.join(", ", failedFiles));
    }

    return uploadedUrls;
  }

  // 단일 파일 삭제
  public void remove(String filename) {
    if (filename == null || filename.trim().isEmpty()) {
      throw new IllegalArgumentException("파일명이 비어있습니다.");
    }

    try {
      if (!s3Client.doesObjectExist(bucketName, filename)) {
        throw new FileNotFoundException("파일을 찾을 수 없습니다: " + filename);
      }
      s3Client.deleteObject(bucketName, filename);
    } catch (Exception e) {
      throw new RuntimeException("파일 삭제 실패: " + e.getMessage());
    }
  }

  // 다중 파일 삭제
  public void remove(List<String> filenames) {
    List<String> failedFiles = new ArrayList<>();

    for (String filename : filenames) {
      try {
        remove(filename);
      } catch (Exception e) {
        failedFiles.add(filename + " (" + e.getMessage() + ")");
      }
    }

    if (!failedFiles.isEmpty()) {
      throw new RuntimeException("다음 파일들의 삭제가 실패했습니다: " + String.join(", ", failedFiles));
    }
  }

  // 고유 파일명 생성 (UUID 사용)
  private String createUniqueFileName(MultipartFile file) {
    String originalFileName = file.getOriginalFilename();
    String extension = originalFileName.substring(originalFileName.lastIndexOf("."));
    return UUID.randomUUID().toString() + extension;
  }

  // 경로 생성 및 검증
  private String generateValidPath(String path) {
    String trimmedPath = path.trim();

    // 빈 문자열인 경우
    if (trimmedPath.isEmpty()) {
      return "";
    }

    // 허용되지 않는 문자 패턴 검사
    String invalidChars = "[\\\\/:*?\"<>|]";
    if (trimmedPath.matches(".*" + invalidChars + ".*")) {
      throw new IllegalArgumentException("경로에 허용되지 않는 특수문자가 포함되어 있습니다.");
    }

    // 경로 끝에 / 추가
    return trimmedPath.endsWith("/") ? trimmedPath : trimmedPath + "/";
  }

  // 파일 메타데이터 생성
  private ObjectMetadata createFileMetadata(MultipartFile file) {
    ObjectMetadata metadata = new ObjectMetadata();
    metadata.setContentType(file.getContentType());
    metadata.setContentLength(file.getSize());
    return metadata;
  }
}
