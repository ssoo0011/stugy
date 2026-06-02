package com.stugy.studygroup.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateStudyGroupRequest(
		@NotBlank @Size(max = 100) String title,
		@NotBlank @Pattern(regexp = "^(개발|영어|자격증|취업|독서|운동)$") String category,
		@NotBlank @Pattern(regexp = "^(온라인|오프라인|혼합)$") String studyMethod,
		@NotBlank @Size(max = 30) String region,
		@Size(max = 100) String place,
		@Min(2) @Max(100) int capacity,
		@NotBlank @Pattern(regexp = "^(입문|초급|중급|고급)$") String difficulty,
		@NotBlank @Size(max = 50) String meetingDays,
		@NotBlank @Size(max = 30) String meetingTime,
		@NotBlank @Size(max = 50) String duration,
		@NotBlank @Size(max = 300) String goal,
		@Size(max = 500) String participationRequirements,
		@NotBlank String description,
		@Size(max = 500) String contactLink
) {
}
