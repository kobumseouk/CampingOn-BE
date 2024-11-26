package site.campingon.campingon.s3bucket.Dto;

import lombok.Getter;

public class S3BucketRemoveRequestDto {
  @Getter
  public static class Filename {
    private String filename;
  }

  @Getter
  public static class Filenames {
    private String[] filename;
  }
}
