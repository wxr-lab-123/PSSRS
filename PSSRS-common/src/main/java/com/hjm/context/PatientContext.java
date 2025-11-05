package com.hjm.context;

import com.hjm.pojo.DTO.PatientDTO;

public class PatientContext {
    public static ThreadLocal<PatientDTO> patient = new ThreadLocal<>();
    public static void saveP(PatientDTO patientDTO){
        patient.set(patientDTO);
    }
    public static PatientDTO getPatient(){
        return patient.get();
    }
    public static void removePatient(){
        patient.remove();
    }
}
