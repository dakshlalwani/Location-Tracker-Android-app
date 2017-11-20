package ssadteam5.vtsapp;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

public class VehicleCardAdapter extends RecyclerView.Adapter<VehicleCardAdapter.MyViewHolder>
{
    private final Context mContext;
    private final List<VehicleCard> vehicleCards;

    public VehicleCardAdapter(Context mContext, List<VehicleCard> vehicleCards)
    {
        this.mContext = mContext;
        this.vehicleCards = vehicleCards;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder
    {
        public final TextView name;
        public final ImageView thumbnail;

        public MyViewHolder(View view)
        {
            super(view);
            name = view.findViewById(R.id.vehicle_name);
            thumbnail = view.findViewById(R.id.vehicle_thumbnail);
        }
    }
    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.vehicle_card, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position)
    {
        final VehicleCard vehicleCard = vehicleCards.get(position);
        holder.thumbnail.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Intent intent = new Intent(mContext, TrackVehicleActivity.class);
                intent.putExtra("deviceName",vehicleCard.getName());
                mContext.startActivity(intent);
            }
        });
        holder.name.setText(vehicleCard.getName());
    }

    @Override
    public int getItemCount()
    {
        return vehicleCards.size();
    }
}
