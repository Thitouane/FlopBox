package com.helle.flopbox;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.ArrayList;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;


/**
 * Root resource (exposed at "server" path)
 */
@Path("server") 
public class ServerService {
    private ArrayList<Server> serveurs = new ArrayList<Server>();
    private FTPClient client;

     /**
     * 
     * @param server
     * @return
     */
    @GET
    @Path("/all")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getServer() {
        return Response
                .status(Response.Status.OK)
                .entity("test")
                .build();
    }

    /**
     * 
     * @param server
     * @return
     */
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Path("{name}")
    public Response insertServer(@PathParam("name") String name, @QueryParam("address") String address) {
        Server server = new Server(name, address);
        if (this.serveurs.add(server)) {
            return Response
                .status(Response.Status.OK)
                .entity(name + " added.")
                .build();
        }
        return Response
                .status(Response.Status.NOT_FOUND)
                .build();
    }

    /**
     * 
     * @param serv
     * @param serverID
     * @return
     */
    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    @Path("{name}")
    public Response modifServer(@PathParam("name") String name, @QueryParam("new_address") String new_address, @QueryParam("new_name") String new_name) {
        for (Server s : this.serveurs) {
            if (s.getName() == name) {
                s.setName(new_name);
                s.setAddress(new_address);
                return Response
                    .status(Response.Status.OK)
                    .entity(s)
                    .build();
            } 
        }
        return Response
                .status(Response.Status.NOT_FOUND)
                .build();
    }

    /**
     * 
     * @param server
     * @return
     */
    @DELETE
    @Path("{name}")
    public Response deleteServer(@PathParam("name") String name) {
        for (Server s : this.serveurs) {
            if (s.getName() == name) {
                if (this.serveurs.remove(s)) {
                    return Response
                        .status(Response.Status.OK)
                        .entity(s)
                        .build();
                } else {
                    return Response
                        .status(Response.Status.NOT_FOUND)
                        .build();
                }
            } 
        }
        return Response
            .status(Response.Status.NOT_FOUND)
            .build();
    }

    /**
     * 
     */
    private void connect(Server server) {
        this.client = new FTPClient();
        try {
            this.client.connect(server.getAddress(), server.getPort());
            this.client.login(server.getUser(), server.getPass());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 
     * @param server
     * @param path
     */
    @GET
    @Path("{name}/{path: .*}")
    @Produces(MediaType.APPLICATION_JSON)
    private Response getList(@PathParam("name") String name, @PathParam("path") String path) {
        StringBuilder filesname = new StringBuilder();
        for(Server s : this.serveurs) {
            if (s.getName() == name) {
                this.connect(s);
                try {
                    if (this.client.isConnected()) {
                        this.client.enterLocalPassiveMode();
                        FTPFile[] files = this.client.listFiles(path);
                        for (FTPFile file : files) {
                            String fname = file.getName();
                            if (file.isDirectory()) {
                                fname = "[" + fname + "]";
                            }
                            fname += "\n";
                            filesname.append(fname);
                        }

                        this.client.disconnect();

                        return Response
                            .status(Response.Status.OK)
                            .entity(filesname)
                            .build();
                    } 
                } catch (IOException e) {
                    return Response
                    .status(Response.Status.INTERNAL_SERVER_ERROR)
                    .build();
                }
            }
        }
        return Response
            .status(Response.Status.NOT_FOUND)
            .build(); 
    }   

    /**
     * 
     * @param server
     * @param path
     */
    @GET
    @Path("{name}/{path: .*}")
    @Produces(MediaType.APPLICATION_JSON)
    private Response getDoc(@PathParam("name") String name, @PathParam("path") String path, @QueryParam("download") Boolean download) {
        if (download) {
            for(Server s : this.serveurs) {
                if (s.getName() == name) {
                    this.connect(s);
                    try {
                        if (this.client.isConnected()) {
                            File file = new File("/doc/" + name + "/" + path);
                            if (!file.exists()) {
                                file.getParentFile().mkdirs();
                                file.createNewFile();
                            }
                            
                            this.client.enterLocalPassiveMode();
                            this.client.setFileType(FTPClient.BINARY_FILE_TYPE);

                            OutputStream stream = new FileOutputStream(file);
                            this.client.retrieveFile(path, stream);

                            this.client.disconnect();

                            return Response
                                .status(Response.Status.OK)
                                .build();
                        }
                    } catch (IOException e) {
                        return Response
                        .status(Response.Status.INTERNAL_SERVER_ERROR)
                        .build();
                    }
                }
            }   
            return Response
            .status(Response.Status.NOT_FOUND)
            .build(); 
        } else {
            return this.getList(name, path);
        }
        
    }

    /**
     * 
     * @param server
     * @param path
     */
    @POST
    @Path("{name}/{path: .*}")
    @Produces(MediaType.APPLICATION_JSON)
    private Response addDoc(@PathParam("name") String name, @PathParam("path") String path) {
        for(Server s : this.serveurs) {
            if (s.getName() == name) {
                this.connect(s);
                try {
                    if (this.client.isConnected() && this.client.makeDirectory(path)) {
                        this.client.disconnect();
                        return Response
                            .status(Response.Status.OK)
                            .build();
                    }

                    this.client.disconnect();
                
                    return Response
                        .status(Response.Status.INTERNAL_SERVER_ERROR)
                        .build();

                } catch (IOException e) {
                    return Response
                            .status(Response.Status.INTERNAL_SERVER_ERROR)
                            .build();
                }
            }
        }
        return Response
            .status(Response.Status.NOT_FOUND)
            .build();
    }

    /**
     * 
     * @param server
     * @param path
     * @return
     */
    @DELETE
    @Path("{name}/{path: .*}")
    @Produces(MediaType.APPLICATION_JSON)
    private Response deleteDoc(@PathParam("name") String name, @PathParam("path") String path) {
        for(Server s : this.serveurs) {
            if (s.getName() == name) {
                this.connect(s);
                try {
                    if (this.client.isConnected()) {
                        this.client.enterLocalPassiveMode();
                        if (this.client.deleteFile(path)) {
                            this.client.disconnect();
                            return Response
                                .status(Response.Status.NO_CONTENT)
                                .build();
                        }
                    }

                    this.client.disconnect();

                    return Response
                        .status(Response.Status.NOT_FOUND)
                        .build();

                } catch (IOException e) {
                    return Response
                        .status(Response.Status.INTERNAL_SERVER_ERROR)
                        .build();
                }
            }
        }
        return Response
            .status(Response.Status.NOT_FOUND)
            .build();
    }

    /**
     * 
     * @param server
     * @param path
     * @return
     */
    @PUT
    @Path("{name}/{path: .*}")
    @Produces(MediaType.APPLICATION_JSON)
    private Response renameDoc(@PathParam("name") String name, @PathParam("path") String path, @QueryParam("new_name") String new_name) {
        for(Server s : this.serveurs) {
            if (s.getName() == name) {
                this.connect(s);
                try {
                    if (this.client.isConnected()) {
                        if (this.client.rename(path, new_name)) {
                            this.client.disconnect();
                            return Response
                                .status(Response.Status.OK)
                                .build();
                        }
                    }
                    this.client.disconnect();
                    return Response
                        .status(Response.Status.NOT_FOUND)
                        .build();
                } catch (IOException e) {
                    return Response
                        .status(Response.Status.INTERNAL_SERVER_ERROR)
                        .build();
                }
            }
        }
        return Response
            .status(Response.Status.NOT_FOUND)
            .build();
    }
    
    
}
