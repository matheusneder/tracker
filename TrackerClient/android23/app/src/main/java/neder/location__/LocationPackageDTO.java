package neder.location;

/**
 * Created by Matheus on 18/10/2016.
 */

public class LocationPackageDTO {
    public LocationPackageDTO(String id, LocationDTO location) {
        this.id = id;
        this.location = location;
    }

    public LocationPackageDTO() { }

    public String id;
    public LocationDTO location;
}
