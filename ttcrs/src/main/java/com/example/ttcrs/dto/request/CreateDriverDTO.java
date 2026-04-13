package com.example.ttcrs.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateDriverDTO {

    @NotBlank(message = "name không được để trống")
    @Size(max = 100, message = "name không được vượt quá 100 ký tự")
    private String name;
}
