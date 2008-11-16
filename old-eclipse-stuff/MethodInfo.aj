//package com.atlassian.theplugin.eclipse.util;
//
//
////public aspect MethodInfo {
////
////	pointcut logInfoBefore() : execution(* com.atlassian..*(..)) 
////		&& ! execution(* com.atlassian.theplugin.eclipse.view.bamboo.BambooToolWindowContent..*(..));
////
////	before() : logInfoBefore() {
////		System.out.println("started: " + thisJoinPoint.toShortString());
//////		Object r = proceed(thisJoinPoint.getArgs());
//////		System.out.println("finished: " + thisJoinPoint.toShortString());
//////		return r;
////	}
////	
////	pointcut logInfoTest() : execution(* com.atlassian.theplugin.eclipse.view.bamboo.BambooToolWindowContent.updateBuildStatuses(..));
////	
////	before() : logInfoTest() {
////		System.out.println("started: " + thisJoinPoint.toShortString());
////	}
////	
////	//pointcut sfsf() : execution(modifier_pattern * BambooToolWindowContent.id_pattern(..));
////}
