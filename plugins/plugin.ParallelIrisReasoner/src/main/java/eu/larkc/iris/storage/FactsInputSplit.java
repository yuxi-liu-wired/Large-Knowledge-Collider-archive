/*
 * Copyright 2010 Softgress - http://www.softgress.com/
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package eu.larkc.iris.storage;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.apache.hadoop.mapred.InputSplit;

public class FactsInputSplit implements InputSplit {

	private long offset = 0;
	private long limit = 0;

	public FactsInputSplit() {
	}
		
	public FactsInputSplit(long limit, long offset) {
		this.limit = limit;
		this.offset = offset;
	}
	
	/** {@inheritDoc} */
	public String[] getLocations() throws IOException {
		return new String[] {};
	}

	public long getLimit() {
		return limit;
	}

	public long getOffset() {
		return offset;
	}

	@Override
	public long getLength() throws IOException {
		return limit;
	}

	@Override
	public void write(DataOutput out) throws IOException {
		out.writeLong(limit);
		out.writeLong(offset);
	}

	@Override
	public void readFields(DataInput in) throws IOException {
		limit = in.readLong();
		offset = in.readLong();
	}

}
