package com.example.ttcrs.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateTrailerDTO {

    @NotBlank(message = "code không được để trống")
    @Size(max = 50, message = "code không được vượt quá 50 ký tự")
    private String code;

    @NotBlank(message = "currentLocationCode không được để trống")
    @Size(max = 50)
    private String currentLocationCode;
}
