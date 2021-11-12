package com.mobcube.keepup.model;

import java.text.DateFormat;
import java.util.Date;

public class Reparos {
    String tipo, id_veiculo;
    Integer km_reparo, duracao, percorrido, percorridoFalta;
    Date data_hora;


    public Reparos(){}

    public Reparos(String tipo, String id_veiculo, Integer km_reparo, Integer duracao, Date data_hora, Integer percorrido, Integer percorridoFalta) {
        this.tipo = tipo;
        this.id_veiculo = id_veiculo;
        this.km_reparo = km_reparo;
        this.duracao = duracao;
        this.data_hora = data_hora;
        this.percorrido = percorrido;
        this.percorridoFalta = percorridoFalta;
    }

    public Integer getPercorrido() {
        return percorrido;
    }

    public void setPercorrido(Integer percorrido) {
        this.percorrido = percorrido;
    }

    public Integer getPercorridoFalta() {
        return percorridoFalta;
    }

    public void setPercorridoFalta(Integer percorridoFalta) {
        this.percorridoFalta = percorridoFalta;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getId_veiculo() {
        return id_veiculo;
    }

    public void setId_veiculo(String id_veiculo) {
        this.id_veiculo = id_veiculo;
    }

    public Integer getKm_reparo() {
        return km_reparo;
    }

    public void setKm_reparo(Integer km_reparo) {
        this.km_reparo = km_reparo;
    }

    public Integer getDuracao() {
        return duracao;
    }

    public void setDuracao(Integer duracao) {
        this.duracao = duracao;
    }

    public Date getData_hora() {
        return data_hora;
    }

    public void setData_hora(Date data_hora) {
        this.data_hora = data_hora;
    }
}
