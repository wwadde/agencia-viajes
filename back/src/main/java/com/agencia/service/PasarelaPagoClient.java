package com.agencia.service;

import com.agencia.domain.PagoProveedor;
import com.agencia.domain.dto.PasarelaCobroRequestDTO;
import com.agencia.domain.dto.PasarelaCobroResponseDTO;

public interface PasarelaPagoClient {

    PagoProveedor proveedor();

    PasarelaCobroResponseDTO crearCobro(PasarelaCobroRequestDTO request);
}
