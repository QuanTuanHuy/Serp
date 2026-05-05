package com.example.ttcrs.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DriverStatusId implements Serializable {
    private Long userId;
    private Long tenantId;
}
