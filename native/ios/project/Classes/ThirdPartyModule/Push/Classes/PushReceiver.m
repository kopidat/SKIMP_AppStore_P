//
//  PushReceiver.m
//  SKIMP_AppStore_M
//
//  Created by Sungoh Kang on 2021/05/28.
//

#import "PushReceiver.h"
#import "Keychain.h"

typedef void(^PushReceiverExtLoadHandler)(BOOL success, NSString *richData, NSError *error);

@interface PushReceiver () <PushManagerDelegate>

@end

@implementation PushReceiver

- (void)dealloc {
    NSLog(@"PushReceiver - dealloc");
}

- (id)init {
    self = [super init];
    if (self) {
        [PushManager defaultManager].enabled = NO;
    }
#ifdef DEBUG
    [[PushManager defaultManager].info changeMode:@"DEV"];
#endif
    
    return self;
}

- (void)manager:(PushManager *)manager didLoadPushInfo:(PushManagerInfo *)pushInfo {
    NSLog(@"PushReceiver - manager didLoadPushInfo : %@", pushInfo);
}

- (void)managerDidRegisterForRemoteNotifications:(PushManager *)manager userInfo:(NSDictionary *)userInfo {
    NSLog(@"PushReceiver - managerDidRegisterForRemoteNotification userInfo : %@", userInfo);
    
    NSMutableDictionary *pushUserInfoDic = [[NSMutableDictionary alloc] init];
    [pushUserInfoDic setObject:[manager.info clientUID] forKey:@"cuid"];
    [pushUserInfoDic setObject:[manager.info pushServiceID] forKey:@"psid"];
    [pushUserInfoDic setObject:[manager.info host] forKey:@"host"];
    
    NSData *data = [NSKeyedArchiver archivedDataWithRootObject:pushUserInfoDic];
    NSString *bundleSeedId = [[NSBundle mainBundle] objectForInfoDictionaryKey:@"AppIdentifierPrefix"];
    
    Keychain *keychain = [[Keychain alloc] initWithService:@"SKIMPAppStoreService" withGroup:[NSString stringWithFormat:@"%@%@", bundleSeedId, @"SKIPartnerStoreInfo"]];
    [keychain insertKey:@"pushUserInfo" withData:data withCompletion:^(BOOL successfulOperation, NSData *data, OSStatus status) {
        NSDictionary *resultDic = (NSDictionary *)[NSKeyedUnarchiver unarchiveObjectWithData:data];
        if (successfulOperation) {
            NSLog(@"successfulOperation true");
            NSLog(@"result Data : %@", resultDic);
            NSLog(@"status : %d", (int)status);
        } else {
            NSLog(@"successfulOperation false");
            NSLog(@"result Data : %@", resultDic);
            NSLog(@"status : %d", (int)status);
        }
    }];
    
    if ([[UIDevice currentDevice].systemVersion floatValue] >= 10.0) {
        UNUserNotificationCenter *center = [UNUserNotificationCenter currentNotificationCenter];
        center.delegate = self;
        [center requestAuthorizationWithOptions:(UNAuthorizationOptionAlert | UNAuthorizationOptionBadge | UNAuthorizationOptionSound) completionHandler:^(BOOL granted, NSError * _Nullable error) {
            if (granted) {
                [center getNotificationSettingsWithCompletionHandler:^(UNNotificationSettings * _Nonnull settings) {
                    NSLog(@"%@", settings);
                }];
            } else {
            }
        }];

    }
}

- (void)manager:(PushManager *)manager didFailToRegisterForRemoteNotificationsWithError:(NSError *)error {
    NSLog(@"PushReceiver - didFailToRegisterForRemoteNotificationWithError error : %@", error);
}

- (void)userNotificationCenter:(UNUserNotificationCenter *)center willPresentNotification:(UNNotification *)notification withCompletionHandler:(void (^)(UNNotificationPresentationOptions))completionHandler {

    completionHandler(UNNotificationPresentationOptionBadge|UNNotificationPresentationOptionSound|UNNotificationPresentationOptionAlert);
}

- (void)manager:(PushManager *)manager didReceiveUserNotification:(NSDictionary *)userInfo status:(NSString *)status messageUID:(NSString *)messageUID {
    NSLog(@"PushReceiver - didReceiverUserNotification : %@ status : %@ messageUID : %@", userInfo, status, messageUID);
    
    NSString *extHTML = [[userInfo objectForKey:@"mps"] objectForKey:@"ext"];
    
    NSLog(@"PushReceiver - extHTML : %@", extHTML);
    
    if (extHTML != nil && ([extHTML hasSuffix:@"_msp.html"] || [extHTML hasSuffix:@"_ext.html"])) {
        NSLog(@"PushReceiver - extHTML 11111");
        [self loadExtData:extHTML handler:^(BOOL success, NSString *richData, NSError *error) {
            NSLog(@"PushReceiver - richData : %@", richData);
            
            NSMutableDictionary *notification = [NSMutableDictionary dictionaryWithDictionary:userInfo];
            NSMutableDictionary *mspData = [NSMutableDictionary dictionaryWithDictionary:[notification objectForKey:@"mps"]];
            [mspData setObject:richData forKey:@"ext"];
            [notification setObject:mspData forKey:@"mps"];
            
            [self onReceiveNotification:[NSDictionary dictionaryWithDictionary:notification] status:status messageUID:messageUID];
        }];
    } else {
        NSLog(@"PushReceiver - extHTML 22222");
        [self onReceiveNotification:userInfo status:status messageUID:messageUID];
    }
}

- (void)loadExtData:(NSString *)extHTML handler:(PushReceiverExtLoadHandler)handler {
    NSURL *url = [NSURL URLWithString:extHTML];
    
    if (!url) {
        handler(NO, extHTML, nil);
        return;
    }
    
    [NSURLConnection sendAsynchronousRequest:[NSURLRequest requestWithURL:[NSURL URLWithString:extHTML]]
                                       queue:[NSOperationQueue mainQueue]
                           completionHandler:^(NSURLResponse *response, NSData *data, NSError *connectionError) {
        if (connectionError != nil) {
            dispatch_async(dispatch_get_main_queue(), ^{
                handler(NO, extHTML, connectionError);
            });
            return;
        }
        
        NSHTTPURLResponse *httpResponse = (NSHTTPURLResponse *)response;
        
        if (httpResponse.statusCode != 200) {
            dispatch_async(dispatch_get_main_queue(), ^{
                handler(NO, extHTML, nil);
            });
            return;
        }
        
        NSString *result = [[NSString alloc] initWithData:data encoding:NSUTF8StringEncoding];
        NSString *richData = [NSString stringWithString:result];
        
        richData = [richData stringByRemovingPercentEncoding];
        
        #if ! __has_feature(objc_arc)
        [result release];
        #endif
        
        dispatch_async(dispatch_get_main_queue(), ^{
            handler(YES, richData, nil);
        });
    }];
}

- (void)onReceiveNotification:(NSDictionary *)notification status:(NSString *)status messageUID:(NSString *)messageUID {
    NSLog(@"pushReciver onReceiveNotification");
    PPNavigationController *navigationController = [PPNavigationController ppNavigationController];
    PPWebViewController *current = (PPWebViewController *)navigationController.currentViewCtrl;

//    while (navigationController.isAnimating) {
//        [[NSRunLoop currentRunLoop] runMode:NSDefaultRunLoopMode beforeDate:[NSDate distantFuture]];
//    }

    NSString *payload = [notification jsonString];
    NSString *pushType = @"APNS";

    [[UIApplication sharedApplication] setApplicationIconBadgeNumber:0];
    
    [current callCbfunction:@"onReceiveNotification" withObjects:@{@"status":status, @"payload":payload, @"type":pushType, @"messageUID":messageUID}, nil];
}

@end
