package com.example.drderma;

public class DiseaseInfoSample {

    private String diseaseName;
    private  String info;
    private  String severity;
    private  String causes;
    private  String cure;

    public String getDiseaseName() {
        return diseaseName;
    }

    public void setDiseaseName(String diseaseName) {
        this.diseaseName = diseaseName;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public String getSeverity() {
        return severity;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }

    public String getCauses() {
        return causes;
    }

    public void setCauses(String causes) {
        this.causes = causes;
    }

    public String getCure() {
        return cure;
    }

    public void setCure(String cure) {
        this.cure = cure;
    }

    @Override
    public String toString() {
        return "DiseaseInfoSample{" +
                "diseaseName='" + diseaseName + '\'' +
                ", info='" + info + '\'' +
                ", severity='" + severity + '\'' +
                ", causes='" + causes + '\'' +
                ", cure='" + cure + '\'' +
                '}';
    }
}
