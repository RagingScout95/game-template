package com.pixelgame.engine.dto;

import com.pixelgame.engine.model.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthPayload {
    private String token;
    private User user;
}

