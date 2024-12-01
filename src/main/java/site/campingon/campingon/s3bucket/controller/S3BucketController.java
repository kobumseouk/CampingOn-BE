package site.campingon.campingon.s3bucket.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import site.campingon.campingon.s3bucket.service.S3BucketService;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/s3/bucket")
@RequiredArgsConstructor
public class S3BucketController {
  private final S3BucketService s3BucketService;

  @PostMapping("/one")
  public ResponseEntity<?> uploadFile(@RequestParam("file") MultipartFile file, @RequestParam("path") String path) {
    try {
      String fileUrl = s3BucketService.upload(file, path);
      return new ResponseEntity<>(fileUrl, HttpStatus.OK);
    } catch (IOException e) {
      return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  @PostMapping("/many")
  public ResponseEntity<?> uploadFiles(@RequestParam("file") List<MultipartFile> files, @RequestParam("path") String path) {
    try {
      List<String> fileUrls = s3BucketService.upload(files, path);
      return new ResponseEntity<>(fileUrls, HttpStatus.OK);
    } catch (IOException e) {
      return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  @DeleteMapping("/one")
  public ResponseEntity<String> deleteFile(@RequestBody String filename) {
    try {
      s3BucketService.remove(filename);
      return new ResponseEntity<>("File deleted successfully", HttpStatus.OK);
    } catch (Exception e) {
      return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  @DeleteMapping("/many")
  public ResponseEntity<?> deleteFiles(@RequestBody List<String> filenames) {
    try {
      s3BucketService.remove(filenames);
      return new ResponseEntity<>("Files deleted successfully", HttpStatus.OK);
    } catch (Exception e) {
      return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }
}

