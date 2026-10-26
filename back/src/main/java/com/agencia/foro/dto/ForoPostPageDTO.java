package com.agencia.foro.dto;

import java.util.List;

public record ForoPostPageDTO(
        List<ForoPostResumenDTO> items,
        Integer page,
        Integer size,
        Long totalItems,
        Integer totalPages,
        Boolean hasNext,
        Boolean hasPrevious
) {
}
