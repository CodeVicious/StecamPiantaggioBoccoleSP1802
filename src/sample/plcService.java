package sample;

import Moka7.S7Client;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class plcService {
    private final S7Client s7client= new S7Client();
    public plcService(){
        ExecutorService service = Executors.newCachedThreadPool();
        service.submit(new Runnable() {
            @Override
            public void run() {
                while(true){
                    //Check S7 Status
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        s7client.Connect();
                    }
                }

            }
        });
    }
    public S7Client getS7client() {
        return s7client;
    }
}
