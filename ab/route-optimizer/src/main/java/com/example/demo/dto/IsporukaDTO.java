package com.example.demo.dto;

import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
public class IsporukaDTO {
    private Long id;
    private Double kolicinaKg;
    private String status;
    private LocalDateTime datumKreiranja;
    private LocalDateTime datumPolaska;
    private LocalDateTime datumDolaska;

    private Long voziloId;
    private String voziloRegistracija;

    private Long vozacId;
    private String vozacIme;
    private String vozacPrezime;

    private Long routeId;
    private String routeNaziv;
}
