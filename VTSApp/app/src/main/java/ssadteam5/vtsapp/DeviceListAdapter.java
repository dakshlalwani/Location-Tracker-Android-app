package ssadteam5.vtsapp;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.List;

public class DeviceListAdapter extends RecyclerView.Adapter<DeviceListAdapter.MyViewHolder>
{
    private final Context mContext;
    private final List<VehicleCard> vehicleCards;

    public DeviceListAdapter(Context mContext, List<VehicleCard> vehicleCards)
    {
        this.mContext = mContext;
        this.vehicleCards = vehicleCards;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder
    {
        public final ImageView thumbnail;
        public final TextView name;
        public final TextView account;
        public final TextView description;
        public final RelativeLayout relativeLayout;
        public MyViewHolder(View view)
        {
            super(view);
            relativeLayout = view.findViewById(R.id.relLayout);
            name = view.findViewById(R.id.name);
            thumbnail = view.findViewById(R.id.icon);
            account = view.findViewById(R.id.account);
            description = view.findViewById(R.id.description);
        }
    }
    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position)
    {
        final VehicleCard vehicleCard = vehicleCards.get(position);

        holder.relativeLayout.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                DeviceDetailsFragment dialog = new DeviceDetailsFragment();
                Bundle bundle = new Bundle();
                bundle.putString("deviceName",vehicleCard.getName());
                bundle.putSerializable("vehicleDetails",vehicleCard.getVehicleDetailsDO().toString());

                dialog.setArguments(bundle);
                dialog.show(((AppCompatActivity)mContext).getSupportFragmentManager(),"dialog");
            }
        });
        holder.name.setText(vehicleCard.getName());
        holder.account.setText(vehicleCard.getAccount());
        holder.description.setText(vehicleCard.getDescription());
    }

    @Override
    public int getItemCount()
    {
        return vehicleCards.size();
    }
}
