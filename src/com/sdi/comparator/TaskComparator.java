package com.sdi.comparator;

import java.util.Comparator;

import com.sdi.dto.Task;

public class TaskComparator implements Comparator<Task> {

	@Override
	public int compare(Task first, Task second) {
		return first.getTitle().compareTo(second.getTitle());
	}

}
