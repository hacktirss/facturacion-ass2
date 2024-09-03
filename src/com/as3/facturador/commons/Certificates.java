package com.as3.facturador.commons;

import com.softcoatl.security.Certificate;
import lombok.Data;

@Data
public class Certificates {

    private Certificate certificate;
    private byte[] image;
}
