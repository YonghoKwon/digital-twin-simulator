package com.dt.digitaltwinsimulator.entity.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
@Getter
@Setter
public class DBConnectionInfoDto {
    private String url;
    private String username;
    private String password;
    private String driverClassName;
}
