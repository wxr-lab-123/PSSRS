package com.hjm.context;

import com.hjm.pojo.DTO.UserPatientDTO;

public class UserPatientContext {
    private static final ThreadLocal<UserPatientDTO> TL = new ThreadLocal<>();
    public static void save(UserPatientDTO dto) { TL.set(dto); }
    public static UserPatientDTO get() { return TL.get(); }
    public static void remove() { TL.remove(); }
}

