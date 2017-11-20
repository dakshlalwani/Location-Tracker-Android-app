package ssadteam5.vtsapp;


import org.json.JSONObject;

class VehicleCard
{
    private final String name;
    private String id;
    private String account;
    private String description;
    private JSONObject vehicleDetailsDO;
    private JSONObject driverDetailsDO;

    public JSONObject getVehicleDetailsDO()
    {
        return vehicleDetailsDO;
    }

    public JSONObject getDriverDetailsDO()
    {
        return driverDetailsDO;
    }



    public String getAccount()
    {
        return account;
    }

    public String getDescription()
    {
        return description;
    }

    public VehicleCard(String name, String account, String description,JSONObject vehicleDetailsDO,JSONObject driverDetailsDO)
    {
        this.name = name;
        this.account = account;
        this.description = description;
        this.vehicleDetailsDO = vehicleDetailsDO;
        this.driverDetailsDO = driverDetailsDO;
    }


    public String getName()
    {
        return name;
    }

    public VehicleCard(String name)
    {
        this.name = name;
    }
}
