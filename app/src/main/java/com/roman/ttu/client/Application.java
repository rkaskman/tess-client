package com.roman.ttu.client;

import dagger.ObjectGraph;

public class Application extends android.app.Application {

    private ObjectGraph objectGraph;

    @Override
    public void onCreate() {
        super.onCreate();
        objectGraph = ObjectGraph.create(new TessModule());
    }

    public ObjectGraph getObjectGraph() {
        return objectGraph;
    }
}
