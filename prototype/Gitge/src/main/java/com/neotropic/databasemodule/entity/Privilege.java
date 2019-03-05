package com.neotropic.databasemodule.entity;

import org.springframework.data.annotation.Id;

/**
 * Entity privilege
 *
 * @author Hardy Ryan Chingal Martinez {@literal <ryan.chingal@kuwaiba.org>}
 */
public class Privilege {

    @Id
    private String id; 
    private String view;
    private boolean read;
    private boolean write;
    private boolean execte;

    public Privilege() {
        super();
    }

    public Privilege(String view, boolean read, boolean write, boolean execte) {
        super();
        this.view = view;
        this.read = read;
        this.write = write;
        this.execte = execte;
    }

    /**
     * @return the view
     */
    public String getView() {
        return view;
    }

    /**
     * @param view the view to set
     */
    public void setView(String view) {
        this.view = view;
    }

    /**
     * @return the read
     */
    public boolean isRead() {
        return read;
    }

    /**
     * @param read the read to set
     */
    public void setRead(boolean read) {
        this.read = read;
    }

    /**
     * @return the write
     */
    public boolean isWrite() {
        return write;
    }

    /**
     * @param write the write to set
     */
    public void setWrite(boolean write) {
        this.write = write;
    }

    /**
     * @return the execte
     */
    public boolean isExecte() {
        return execte;
    }

    /**
     * @param execte the execte to set
     */
    public void setExecte(boolean execte) {
        this.execte = execte;
    }

    /**
     * @return the id
     */
    public String getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(String id) {
        this.id = id;
    }

}
