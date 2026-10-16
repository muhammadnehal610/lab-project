package org.example.common.response;

import java.util.List;

public class StudentProblemResponse {
    private List<StudentProblemData> data;
    private int totalRecords;
    private int totalSolved;

    public StudentProblemResponse() {}

    public StudentProblemResponse(List<StudentProblemData> data, int totalRecords, int totalSolved) {
        this.data = data;
        this.totalRecords = totalRecords;
        this.totalSolved = totalSolved;
    }

    public List<StudentProblemData> getData() {
        return data;
    }

    public void setData(List<StudentProblemData> data) {
        this.data = data;
    }

    public int getTotalRecords() {
        return totalRecords;
    }

    public void setTotalRecords(int totalRecords) {
        this.totalRecords = totalRecords;
    }

    public int getTotalSolved() {
        return totalSolved;
    }

    public void setTotalSolved(int totalSolved) {
        this.totalSolved = totalSolved;
    }
}