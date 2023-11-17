package br.ufsm.csi.tapw.pilacoin.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Pilacoin  {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
}
