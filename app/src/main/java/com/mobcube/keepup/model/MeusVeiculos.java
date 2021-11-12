package com.mobcube.keepup.model;

public class MeusVeiculos {

    String id_veiculo, marca, modelo, apelido, tipo, id_user;
    Integer ano, km_inicial, km_atual;

    public Integer getKm_atual() {
        return km_atual;
    }

    public void setKm_atual(Integer km_atual) {
        this.km_atual = km_atual;
    }

    public MeusVeiculos(){}

    public MeusVeiculos(String id_veiculo, String marca, String modelo, String apelido, String tipo, String id_user, Integer ano, Integer km_inicial, Integer km_atual) {
        this.id_veiculo = id_veiculo;
        this.marca = marca;
        this.modelo = modelo;
        this.apelido = apelido;
        this.tipo = tipo;
        this.id_user = id_user;
        this.ano = ano;
        this.km_inicial = km_inicial;
        this.km_atual = km_atual;
    }

    public String getId_veiculo() {
        return id_veiculo;
    }

    public String getMarca() {
        return marca;
    }

    public void setMarca(String marca) {
        this.marca = marca;
    }

    public String getModelo() {
        return modelo;
    }

    public void setModelo(String modelo) {
        this.modelo = modelo;
    }

    public String getApelido() {
        return apelido;
    }

    public void setApelido(String apelido) {
        this.apelido = apelido;
    }

    public String getTipo() {
        return tipo;
    }

    public void setId_veiculo(String id_veiculo) {
        this.id_veiculo = id_veiculo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getId_user() {
        return id_user;
    }

    public void setId_user(String id_user) {
        this.id_user = id_user;
    }

    public Integer getAno() {
        return ano;
    }

    public void setAno(Integer ano) {
        this.ano = ano;
    }

    public Integer getKm_inicial() {
        return km_inicial;
    }

    public void setKm_inicial(Integer km_inicial) {
        this.km_inicial = km_inicial;
    }
}
