package com.agencia.paquete.mapper;

import com.agencia.destino.mapper.DestinoMapper;
import com.agencia.paquete.dto.ImagenPaqueteDTO;
import com.agencia.paquete.dto.PaqueteDetalleDTO;
import com.agencia.paquete.dto.PaqueteResumenDTO;
import com.agencia.paquete.dto.SalidaDTO;
import com.agencia.paquete.entity.ImagenPaquete;
import com.agencia.paquete.entity.Paquete;
import com.agencia.paquete.entity.Salida;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.Comparator;
import java.util.List;

@Mapper(componentModel = "spring", uses = DestinoMapper.class)
public interface PaqueteMapper {

    @Mapping(target = "destino", source = ".", qualifiedByName = "destinoPrincipalNombre")
    @Mapping(target = "ciudad", source = ".", qualifiedByName = "destinoPrincipalCiudad")
    @Mapping(target = "pais", source = ".", qualifiedByName = "destinoPrincipalPais")
    @Mapping(target = "imagenPrincipal", source = ".", qualifiedByName = "imagenPrincipal")
    PaqueteResumenDTO toResumenDTO(Paquete paquete);

    @Mapping(target = "destino", source = ".", qualifiedByName = "destinoPrincipal")
    @Mapping(target = "destinos", source = "destinos")
    @Mapping(target = "imagenes", source = "imagenes", qualifiedByName = "imagenesOrdenadas")
    @Mapping(target = "salidas", source = "salidas", qualifiedByName = "salidas")
    PaqueteDetalleDTO toDetalleDTO(Paquete paquete);

    ImagenPaqueteDTO toImagenDTO(ImagenPaquete imagen);

    SalidaDTO toSalidaDTO(Salida salida);

    @Named("destinoPrincipal")
    default com.agencia.destino.entity.Destino destinoPrincipal(Paquete paquete) {
        return paquete == null || paquete.getDestinos() == null || paquete.getDestinos().isEmpty()
                ? null : paquete.getDestinos().get(0);
    }

    @Named("destinoPrincipalNombre")
    default String destinoPrincipalNombre(Paquete paquete) {
        return destinoPrincipal(paquete) == null ? null : destinoPrincipal(paquete).getNombre();
    }

    @Named("destinoPrincipalCiudad")
    default String destinoPrincipalCiudad(Paquete paquete) {
        return destinoPrincipal(paquete) == null ? null : destinoPrincipal(paquete).getCiudad();
    }

    @Named("destinoPrincipalPais")
    default String destinoPrincipalPais(Paquete paquete) {
        return destinoPrincipal(paquete) == null ? null : destinoPrincipal(paquete).getPais();
    }

    @Named("imagenPrincipal")
    default String imagenPrincipal(Paquete paquete) {
        if (paquete == null || paquete.getImagenes() == null || paquete.getImagenes().isEmpty()) {
            return null;
        }
        return paquete.getImagenes().stream().filter(ImagenPaquete::isPrincipal).min(Comparator.comparing(this::orden)).or(() -> paquete.getImagenes().stream().min(Comparator.comparing(this::orden))).map(ImagenPaquete::getUrl).orElse(null);
    }

    @Named("imagenesOrdenadas")
    default List<ImagenPaqueteDTO> imagenesOrdenadas(List<ImagenPaquete> imagenes) {
        if (imagenes == null) {
            return List.of();
        }
        return imagenes.stream().sorted(Comparator.comparing(this::orden)).map(this::toImagenDTO).toList();
    }

    @Named("salidas")
    default List<SalidaDTO> salidas(List<Salida> salidas) {
        if (salidas == null) {
            return List.of();
        }
        return salidas.stream().map(this::toSalidaDTO).toList();
    }

    default int orden(ImagenPaquete imagen) {
        return imagen.getOrden() == null ? Integer.MAX_VALUE : imagen.getOrden();
    }
}