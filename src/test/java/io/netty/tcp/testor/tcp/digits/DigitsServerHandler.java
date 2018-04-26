/**
 * netty-tcp.
 * Copyright (C) 1999-2017, All rights reserved.
 *
 * This program and the accompanying materials are under the terms of the Apache License Version 2.0.
 */
package io.netty.tcp.testor.tcp.digits;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.channel.Channel;
import io.netty.tcp.server.ServiceAppHandler;

/**
 * Echo server.
 * 
 * @author Lawnstein.Chan
 * @version $Revision:$
 */
public class DigitsServerHandler implements ServiceAppHandler {
	protected final static Logger logger = LoggerFactory.getLogger(DigitsServerHandler.class);

	protected final static String expv = "<?xml version=\"1.0\" encoding=\"GB18030\"?><Message><Header><MACBRN>I01</MACBRN><PINSED>9277667181726681</PINSED><CHNSRC>110</CHNSRC><CHNDRC>110</CHNDRC><ENCRFG>0</ENCRFG><TRWHPW>1</TRWHPW><PFTSFG>0</PFTSFG><MFSERV>lt</MFSERV><DTPKSQ>1</DTPKSQ><KYVSNO>0</KYVSNO><CHANEL>I01</CHANEL><LGRPCD>9999</LGRPCD><MSTKNO>0060850414105729</MSTKNO><MSGTYP>XML</MSGTYP><SOUADD>192.168.1.243</SOUADD><GOLADD>00000000</GOLADD><STSBFG>0</STSBFG><TMNLCD>J0000</TMNLCD><BZBRCH>2011801</BZBRCH><TLECOD>QK001</TLECOD><TRCODE>36003</TRCODE><TRANMD>1</TRANMD><FRNTCD>36003</FRNTCD><FRNTDT></FRNTDT><FRNTSN></FRNTSN></Header><Body><cuacno>6210880880110000001</cuacno><acctsq>0001</acctsq><bbindt>19990101</bbindt><matudt>20170618</matudt><frbgsq>1</frbgsq><qrycnt>1</qrycnt><cycode>01</cycode></Body></Message>";
	protected final static String repv = "<?xml version=\"1.0\" encoding=\"GB18030\"?><Message><Header><RQASTP>O</RQASTP><MACBRN>I01</MACBRN><PINSED>9277667181726681</PINSED><KYVSNO>0</KYVSNO><CHANEL>I01</CHANEL><CHNSRC>110</CHNSRC><CHNDRC>110</CHNDRC><ENCRFG>0</ENCRFG><TRWHPW>1</TRWHPW><PFTSFG>0</PFTSFG><MSTKNO>0060850414105729</MSTKNO><SOUADD>192.168.1.243</SOUADD><STSBFG>0</STSBFG><FRNTCD>36003</FRNTCD><FRNTDT></FRNTDT><FRNTSN></FRNTSN><TRCODE>36003</TRCODE><TRSBCD></TRSBCD><TRANMD>1</TRANMD><TRDATE>20170604</TRDATE><TRTIME>154302</TRTIME><TLERSN></TLERSN><WARNIF></WARNIF><TRRPCD>00000000</TRRPCD></Header><Body><custno>018011000001</custno><cuacno>6210880880110000001</cuacno><cuacnm>Cust018011000001</cuacnm><total1>37</total1><f360031><Map><dpacct>9999801R10000001</dpacct><pdinfo></pdinfo><trdate>20170604</trdate><pttram>10000</pttram><acblnc>650000</acblnc><trcode>36009</trcode><tlecod>P2000</tlecod><authtl></authtl><tlersn>P20000000002160</tlersn><smryif></smryif><voucsq></voucsq><trtime>202220</trtime><cycode>01</cycode><carefg></carefg><ctnoac>68186080110000001</ctnoac><opsnam>Cust018011000001</opsnam><sequ02>1</sequ02><cuacno>6210880880110000001</cuacno><smrycd>MB5101</smrycd><agetps></agetps><agpstp></agpstp><agpsid></agpsid><chanel>I01</chanel><drcrfg>1</drcrfg><moinf1></moinf1><prdcde>1000</prdcde><deptyp>00</deptyp><sernub>22</sernub><acpfsn>0001</acpfsn><dramot>0</dramot><cramot>0</cramot><rvflg2>0</rvflg2><prtif1></prtif1><prtif2>10000</prtif2><bzbrch>2011801</bzbrch><blflnm>GENLBL</blflnm></Map></f360031><prflag></prflag><rppath></rppath><qryrag></qryrag><chgflg></chgflg><dpevnt></dpevnt></Body></Message>";
	protected final static byte[] repvBytes = repv.getBytes();
	/**
	 * 
	 */
	public DigitsServerHandler() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.csii.ccbs.tcp.netty.server.ServiceAppHandler#call(java.lang.Object,
	 * io.netty.channel.Channel)
	 */
	@Override
	public Object call(Object request, Channel channel) {		
		String input = new String((byte[]) request);
		if (input.equals(expv))
			return repvBytes;
		
		logger.debug("Request > " + input);
		String output = "Received(" + UUID.randomUUID().toString() + "," + System.currentTimeMillis() + "):" + input;
		logger.debug("Response > " + output);
		return output.getBytes();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.csii.ccbs.tcp.netty.server.ServiceAppHandler#onChannelClosed(io.netty
	 * .channel.Channel)
	 */
	@Override
	public void onChannelClosed(Channel channel) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.csii.ccbs.tcp.netty.server.ServiceAppHandler#exceptionCaught(io.netty
	 * .channel.Channel, java.lang.Throwable)
	 */
	@Override
	public void onChannelException(Channel channel, Throwable cause) {
	}

}
