package br.ufsm.csi.tapw.pilacoin.model.json;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Data;

import java.util.Date;

@Data
@JsonDeserialize
public class DificuldadeJson {
    private String dificuldade;
    private Date inicio;
    private Date validadeFinal;
}
