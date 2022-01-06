package com.github.crayonxiaoxin.ppjoke.model;

public class TestUser {
    public DataDTO data;

    public static class DataDTO {
        public String id;
        public String faceID;
        public String userName;
        public Long createTime;
        public Long updateTime;
        public String faceFeature;
        public String authority;
        public Object password;
        public String sessionID;
        public String department;
        public String groupID;
        public String currentStation;
        public Object currentMachine;
        public String currentStationCode;
        public String identityCard;
        public Boolean deleted;

        @Override
        public String toString() {
            return "DataDTO{" +
                    "id='" + id + '\'' +
                    ", faceID='" + faceID + '\'' +
                    ", userName='" + userName + '\'' +
                    ", createTime=" + createTime +
                    ", updateTime=" + updateTime +
                    ", faceFeature='" + faceFeature + '\'' +
                    ", authority='" + authority + '\'' +
                    ", password=" + password +
                    ", sessionID='" + sessionID + '\'' +
                    ", department='" + department + '\'' +
                    ", groupID='" + groupID + '\'' +
                    ", currentStation='" + currentStation + '\'' +
                    ", currentMachine=" + currentMachine +
                    ", currentStationCode='" + currentStationCode + '\'' +
                    ", identityCard='" + identityCard + '\'' +
                    ", deleted=" + deleted +
                    '}';
        }
    }
}
