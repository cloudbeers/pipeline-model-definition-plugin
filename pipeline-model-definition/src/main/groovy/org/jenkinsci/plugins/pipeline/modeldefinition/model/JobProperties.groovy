/*
 * The MIT License
 *
 * Copyright (c) 2016, CloudBees, Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */


package org.jenkinsci.plugins.pipeline.modeldefinition.model

import com.google.common.cache.LoadingCache
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings
import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import hudson.model.JobProperty
import hudson.model.JobPropertyDescriptor
import org.jenkinsci.plugins.pipeline.modeldefinition.Utils
import org.jenkinsci.plugins.scriptsecurity.sandbox.whitelists.Whitelisted
import org.jenkinsci.plugins.structs.describable.UninstantiatedDescribable
import org.jenkinsci.plugins.workflow.cps.CpsScript

import javax.annotation.Nonnull

/**
 * Container for job properties.
 *
 * @author Andrew Bayer
 */
@ToString
@EqualsAndHashCode
@SuppressFBWarnings(value="SE_NO_SERIALVERSIONID")
public class JobProperties implements Serializable, MethodsToList<JobProperty> {
    // Transient since JobProperty isn't serializable. Doesn't really matter since we're in trouble if we get interrupted
    // anyway.
    transient List<JobProperty> properties = []

    public JobProperties(List<JobProperty> props) {
        this.properties = props
    }
    private static final Object CACHE_KEY = new Object()

    private static final LoadingCache<Object,Map<String,String>> propertyTypeCache =
        Utils.generateTypeCache(JobPropertyDescriptor.class, false, ["pipelineTriggers", "parameters"])


    protected Object readResolve() throws IOException {
        // Need to make sure properties is initialized on deserialization, even if it's going to be empty.
        this.properties = []
        return this;
    }

    /**
     * Get a map of allowed property type keys to their actual type ID. If a {@link org.jenkinsci.Symbol} is on the descriptor for a given
     * job property, use that as the key. Otherwise, use the class name.
     *
     * @return A map of valid property type keys to their actual type IDs.
     */
    @Whitelisted
    public static Map<String,String> getAllowedPropertyTypes() {
        return propertyTypeCache.get(CACHE_KEY)
    }

    /**
     * Given a property type key, get the actual type ID.
     *
     * @param key The key to look up.
     * @return The type ID for that key, if it's in the property types cache.
     */
    @Whitelisted
    public static String typeForKey(@Nonnull String key) {
        return getAllowedPropertyTypes().get(key)
    }
}
