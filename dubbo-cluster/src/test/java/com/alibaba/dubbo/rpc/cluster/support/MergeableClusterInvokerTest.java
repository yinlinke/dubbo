/*
 * Copyright 1999-2011 Alibaba Group.
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
package com.alibaba.dubbo.rpc.cluster.support;

import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.Result;
import com.alibaba.dubbo.rpc.RpcResult;
import com.alibaba.dubbo.rpc.cluster.Directory;
import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:gang.lvg@alibaba-inc.com">kimi</a>
 */
public class MergeableClusterInvokerTest {

    private Directory directory = EasyMock.createMock( Directory.class );
    private Invoker firstInvoker = EasyMock.createMock( Invoker.class );
    private Invoker secondInvoker = EasyMock.createMock( Invoker.class );
    private Invocation invocation = EasyMock.createMock( Invocation.class );

    private MergeableClusterInvoker<MenuService> mergeableClusterInvoker;
    
    private Map<String, List<String>> firstMenuMap = new HashMap<String, List<String>>() {
        {
            put( "1", new ArrayList<String>() {
                {
                    add( "10" );
                    add( "11" );
                    add( "12" );
                }
            } );
            put( "2", new ArrayList<String>() {

                {
                    add( "20" );
                    add( "21" );
                    add( "22" );
                }
            } );
        }
    };
    private Map<String, List<String>> secondMenuMap = new HashMap<String, List<String>>() {
        {
            put( "2", new ArrayList<String>() {

                {
                    add( "23" );
                    add( "24" );
                    add( "25" );
                }
            } );
            put( "3", new ArrayList<String>() {

                {
                    add( "30" );
                    add( "31" );
                    add( "32" );
                }
            } );
        }
    };
    
    private Menu firstMenu = new Menu( firstMenuMap );
    private Menu secondMenu = new Menu( secondMenuMap );
    
    private URL url = URL.valueOf( new StringBuilder( 32 )
                                           .append( "test://test/" )
                                           .append( MenuService.class.getName() ).toString() );
    
    @Before
    public void setUp() throws Exception {

        directory = EasyMock.createMock( Directory.class );
        firstInvoker = EasyMock.createMock( Invoker.class );
        secondInvoker = EasyMock.createMock( Invoker.class );
        invocation = EasyMock.createMock( Invocation.class );

    }

    @Test
    public void testGetMenuSuccessfully() throws Exception {

        // setup
        url = url.addParameter( Constants.MERGER_KEY, "merge" );

        EasyMock.expect( invocation.getMethodName() ).andReturn( "getMenu" ).anyTimes();
        EasyMock.expect( invocation.getParameterTypes() ).andReturn( new Class<?>[]{ } ).anyTimes();
        EasyMock.expect( invocation.getArguments() ).andReturn( new Object[]{ } ).anyTimes();
        EasyMock.expect( invocation.getAttachments() ).andReturn( new HashMap<String, String>() )
                .anyTimes();
        EasyMock.expect( invocation.getUrl() ).andReturn( url ).anyTimes();
        EasyMock.replay( invocation );

        EasyMock.expect( firstInvoker.getUrl() ).andReturn(
                url.addParameter( Constants.GROUP_KEY, "first" ) ).anyTimes();
        EasyMock.expect( firstInvoker.getInterface() ).andReturn( MenuService.class ).anyTimes();
        EasyMock.expect( firstInvoker.invoke( invocation ) ).andReturn(
                new RpcResult( firstMenu ) ).anyTimes();
        EasyMock.replay( firstInvoker );

        EasyMock.expect( secondInvoker.getUrl() ).andReturn(
                url.addParameter( Constants.GROUP_KEY, "second" ) ).anyTimes();
        EasyMock.expect( secondInvoker.getInterface() ).andReturn( MenuService.class ).anyTimes();
        EasyMock.expect( secondInvoker.invoke( invocation ) ).andReturn(
                new RpcResult( secondMenu ) ).anyTimes();
        EasyMock.replay( secondInvoker );

        EasyMock.expect( directory.list( invocation ) ).andReturn( new ArrayList() {

            {
                add( firstInvoker );
                add( secondInvoker );
            }
        } ).anyTimes();
        EasyMock.expect( directory.getUrl() ).andReturn( url ).anyTimes();
        EasyMock.expect( directory.getInterface() ).andReturn( MenuService.class ).anyTimes();
        EasyMock.replay( directory );

        mergeableClusterInvoker = new MergeableClusterInvoker<MenuService>( directory );

        // FIXME
        // invoke
        /*Result result = mergeableClusterInvoker.invoke( invocation );
        Assert.assertTrue( result.getResult() instanceof Menu );
        Menu menu = ( Menu ) result.getResult();
        Map<String, List<String>> expected = new HashMap<String, List<String>>();
        merge( expected, firstMenuMap );
        merge( expected, secondMenuMap );
        Assert.assertEquals( expected, menu.getMenus() );*/

    }

    @Test
    public void testAddMenu() throws Exception {

        String menu = "first";
        List<String> menuItems = new ArrayList<String>(){
            {
                add( "1" );
                add( "2" );
            }
        };
        
        EasyMock.expect( invocation.getMethodName() ).andReturn( "addMenu" ).anyTimes();
        EasyMock.expect( invocation.getParameterTypes() ).andReturn(
                new Class<?>[]{ String.class, List.class } ).anyTimes();
        EasyMock.expect( invocation.getArguments() ).andReturn( new Object[]{ menu, menuItems } )
                .anyTimes();
        EasyMock.expect( invocation.getAttachments() ).andReturn( new HashMap<String, String>() )
                .anyTimes();
        EasyMock.expect( invocation.getUrl() ).andReturn( url ).anyTimes();
        EasyMock.replay( invocation );

        EasyMock.expect( firstInvoker.getUrl() ).andReturn(
                url.addParameter( Constants.GROUP_KEY, "first" ) ).anyTimes();
        EasyMock.expect( firstInvoker.getInterface() ).andReturn( MenuService.class ).anyTimes();
        EasyMock.expect( firstInvoker.invoke( invocation ) ).andReturn( new RpcResult() )
                .anyTimes();
        EasyMock.replay( firstInvoker );

        EasyMock.expect( secondInvoker.getUrl() ).andReturn(
                url.addParameter( Constants.GROUP_KEY, "second" ) ).anyTimes();
        EasyMock.expect( secondInvoker.getInterface() ).andReturn( MenuService.class ).anyTimes();
        EasyMock.expect( secondInvoker.invoke( invocation ) ).andReturn(new RpcResult() )
                .anyTimes();
        EasyMock.replay( secondInvoker );

        EasyMock.expect( directory.list( invocation ) ).andReturn( new ArrayList() {

            {
                add( firstInvoker );
                add( secondInvoker );
            }
        } ).anyTimes();
        EasyMock.expect( directory.getUrl() ).andReturn( url ).anyTimes();
        EasyMock.expect( directory.getInterface() ).andReturn( MenuService.class ).anyTimes();
        EasyMock.replay( directory );

        mergeableClusterInvoker = new MergeableClusterInvoker<MenuService>( directory );
        
        // FIXME
        //Result result = mergeableClusterInvoker.invoke( invocation );
        //Assert.assertNull( result.getResult() );

    }

    static void merge( Map<String, List<String>> first, Map<String, List<String>> second ) {
        for( Map.Entry<String, List<String>> entry : second.entrySet() ) {
            List<String> value = first.get( entry.getKey() );
            if ( value != null ) {
                value.addAll( entry.getValue() );
            } else {
                first.put( entry.getKey(), entry.getValue() );
            }
        }
    }

}