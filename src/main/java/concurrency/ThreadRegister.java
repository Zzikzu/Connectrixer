package concurrency;

import java.util.ArrayList;
import java.util.Collections;

public class ThreadRegister {

    private static ThreadRegister instance;
    private ArrayList<Thread> threadList;

    private ThreadRegister() {
        threadList = new ArrayList<Thread>();
    }

    public static ThreadRegister getInstance() {
        if (instance == null) {
            instance = new ThreadRegister();
        }
        return instance;
    }

    public void put(Thread thread){
        threadList.add(thread);
        System.out.println("Thread added");
    }

    public void killAll(){
        Collections.reverse(threadList);



        if (threadList.isEmpty()){
            System.out.println("No running processes");
        }else {
            System.out.println("Stopping running processes");
            for (Thread thread : threadList){
                while (thread.isAlive()){
                    thread.interrupt();
                    if (thread.isInterrupted()){
                        System.out.println("Interupted");
                    }
                }

                if (!thread.isAlive()){
                    threadList.remove(thread);
                    System.out.println("Thread killed");
                }

            }
            System.out.println("Process stopped by user");
        }



    }

}
