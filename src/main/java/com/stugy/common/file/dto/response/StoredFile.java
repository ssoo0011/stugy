package com.stugy.common.file.dto.response;

import org.springframework.core.io.Resource;

public record StoredFile(Resource resource, String contentType, String originalName) {
}
