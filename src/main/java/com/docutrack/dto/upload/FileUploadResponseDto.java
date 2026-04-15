package com.docutrack.dto.upload;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class FileUploadResponseDto {
  String fileName;
  String url;
}

