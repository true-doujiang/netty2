package com.cn.entity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.cn.core.Serializer;

public class Player extends Serializer {
	
	private long playerId;
	
	private int age;

    private String name;
	
	private List<Integer> skills = new ArrayList<>();

	private Map<String, String> lanauge = new HashMap<>();


	private Resource resource = new Resource();

    private Resource nullResource;

    /**
     * 序列化方法
     */
    @Override
    protected void read() {
        this.playerId = readLong();
        this.age = readInt();
        this.name = readString();
        this.skills = readList(Integer.class);
        this.lanauge = readMap(String.class, String.class);
        this.resource = read(Resource.class);
        this.nullResource = read(Resource.class);
    }

    /**
     * 反序列化方法
     */
    @Override
    protected void write() {
        writeLong(playerId);
        writeInt(age);
        writeString(name);
        writeList(skills);
        writeMap(lanauge);
        writeObject(resource);
        writeObject(nullResource);
    }


    public Resource getResource() {
		return resource;
	}

	public void setResource(Resource resource) {
		this.resource = resource;
	}

	public long getPlayerId() {
		return playerId;
	}

	public void setPlayerId(long playerId) {
		this.playerId = playerId;
	}

	public int getAge() {
		return age;
	}

	public void setAge(int age) {
		this.age = age;
	}

	public List<Integer> getSkills() {
		return skills;
	}

	public void setSkills(List<Integer> skills) {
		this.skills = skills;
	}

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Map<String, String> getLanauge() {
        return lanauge;
    }

    public void setLanauge(Map<String, String> lanauge) {
        this.lanauge = lanauge;
    }

    public Resource getNullResource() {
        return nullResource;
    }

    public void setNullResource(Resource nullResource) {
        this.nullResource = nullResource;
    }
}
