package ash.quadmap_android;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.ExecutionException;

public class Interface extends AppCompatActivity {

    private Location Home;
    public int mode = 1;
    String IP;
    int port;
    Socket socket;
    PrintWriter out;
    BufferedWriter bw;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_interface);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(socket != null) {
                    try {
                        bw.write("LAND");
                        bw.newLine();
                        bw.flush();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    Toast.makeText(Interface.this, "Landing Quad", Toast.LENGTH_SHORT).show();
                }
            }
        });
        getSupportFragmentManager().beginTransaction().replace(R.id.include,new MapsActivity()).commit();
        Bundle bundle = getIntent().getExtras();
        IP = bundle.getString("IP");
        port = bundle.getInt("Port");
        Client client = new Client(this,IP,port);
        try {
            client.execute();
            bw = client.get();
            Toast.makeText(this, bw.toString(), Toast.LENGTH_SHORT).show();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        if(bw != null) {
            Toast.makeText(this, "Successfully Connected to Server", Toast.LENGTH_SHORT).show();
        }
        else
            Toast.makeText(this, "Failed to connect to Server.\n" +
                    "        Try Again !!", Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_interface,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if(id == R.id.point_follow){
            mode = 1;
            Toast.makeText(this, "Switched to Point Follow", Toast.LENGTH_SHORT).show();
        }
        if(id == R.id.path_follow){
            mode = 2;
            Toast.makeText(this, "Switched to Path Follow", Toast.LENGTH_SHORT).show();
        }
        if(id == R.id.Go_Home){
            if(bw != null){
                Toast.makeText(this, "Quad coming back to your location.", Toast.LENGTH_SHORT).show();
                try {
                    bw.write("H"+","+Home.getLatitude()+","+Home.getLongitude());
                    bw.newLine();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return super.onOptionsItemSelected(item);
    }

    public void setHome(Location location){
        Home = location;
        Log.i("Home","Updated current location");
    }
    public void GotoPoint(Location[] _location) throws IOException {
        Location location;
        if(mode == 1) {
            location = _location[0];
            Toast.makeText(this, "Going to next Point\n" + location.getLatitude() + "," + location.getLongitude(), Toast.LENGTH_SHORT).show();
            if (bw != null)
                bw.write("A"+","+location.getLatitude() + "," + location.getLongitude());
                bw.newLine();
        }
        else {
            Toast.makeText(this, "Writing Array to Server" , Toast.LENGTH_SHORT).show();
            if(bw != null) {
                for (Location a_location : _location) {
                    bw.write("B" + "," + a_location.getLatitude() + "," + a_location.getLongitude());
                    bw.newLine();
                }
                bw.write("X");
                bw.newLine();
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Intent i = new Intent(this,GPSProvider.class);
        stopService(i);
    }

    public int getMode(){
        return mode;
    }

}
