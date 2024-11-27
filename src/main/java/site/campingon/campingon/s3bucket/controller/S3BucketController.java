package site.campingon.campingon.s3bucket.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import site.campingon.campingon.s3bucket.Dto.S3BucketRemoveRequestDto;
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
  public ResponseEntity<String> deleteFile(@RequestBody S3BucketRemoveRequestDto.Filename req) {
    try {
      s3BucketService.remove(req.getFilename());
    } catch (Exception e) {
      return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }
    return new ResponseEntity<>("Hello World!", HttpStatus.OK);
  }

  @DeleteMapping("/many")
  public ResponseEntity<?> deleteFiles(@RequestBody S3BucketRemoveRequestDto.Filenames req) {
    try {
      s3BucketService.remove(Arrays.asList(req.getFilename()));
      return new ResponseEntity<>("Files deleted successfully", HttpStatus.OK);
    } catch (Exception e) {
      return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }
}

