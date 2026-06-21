package com.finances.dashboard.dto.request;

import org.springframework.web.multipart.MultipartFile;

public record UserProfilePictureRequest(
    MultipartFile file
) {

}
