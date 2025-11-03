package com.financeportal.model;

public class Branch {
    private int branchID;
    private String name;
    private String address;
    private int capacity;
    private String manager;
    private String contact;

    public int getBranchID() { return branchID; }
    public void setBranchID(int branchID) { this.branchID = branchID; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public int getCapacity() { return capacity; }
    public void setCapacity(int capacity) { this.capacity = capacity; }

    public String getManager() { return manager; }
    public void setManager(String manager) { this.manager = manager; }

    public String getContact() { return contact; }
    public void setContact(String contact) { this.contact = contact; }
}
