package com.hjm.pojo.DTO;

import lombok.Data;

import java.util.List;

@Data
public class RoleBindRequest { List<String> permissions; Long version; }
