//
//  HttpCustomNetworkManager.h
//  SKIMP_DemoApp
//
//  Created by Sungoh Kang on 2021/06/21.
//

#import <Foundation/Foundation.h>

@protocol HttpCustomNetworkManagerDelegate<PPNetworkManagerDelegate>

@end

@interface HttpCustomNetworkManager : PPAbstractHttpNetworkManager<PPHttpProviderDelegate>

@end
