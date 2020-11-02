package neder.trackerclient;

public class AuditData {
    public float distance;
    public float lagInSeconds;
    public float deviceAvgSpeed;
    public float estimatedDistanceCompensation;
    public float compensatedDistance;

    public AuditData() {}

    public AuditData(float distance, float lagInSeconds, float deviceAvgSpeed, float estimatedDistanceCompensation, float compensatedDistance) {
        this.distance = distance;
        this.lagInSeconds = lagInSeconds;
        this.deviceAvgSpeed = deviceAvgSpeed;
        this.estimatedDistanceCompensation = estimatedDistanceCompensation;
        this.compensatedDistance = compensatedDistance;
    }
}
