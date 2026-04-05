package com.agencia.service;

import com.agencia.domain.PagoProveedor;
import com.agencia.exception.ReglaNegocioException;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Component
public class PasarelaPagoClientFactory {

    private final Map<PagoProveedor, PasarelaPagoClient> clientsByProvider;

    public PasarelaPagoClientFactory(List<PasarelaPagoClient> clients) {
        this.clientsByProvider = new EnumMap<>(PagoProveedor.class);
        for (PasarelaPagoClient client : clients) {
            this.clientsByProvider.put(client.proveedor(), client);
        }
    }

    public PasarelaPagoClient getClient(PagoProveedor proveedor) {
        PasarelaPagoClient client = clientsByProvider.get(proveedor);
        if (client == null) {
            throw new ReglaNegocioException("No existe implementación de pasarela para el proveedor: " + proveedor);
        }
        return client;
    }
}

