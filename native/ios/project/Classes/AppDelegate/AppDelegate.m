//
//  AppDelegate.m
//

#import "AppDelegate.h"
#import "PushReceiver.h"
#import <MPush/PushManager.h>
#import <MPush/AppDelegate+PushManager.h>

@interface AppDelegate ()

@property (strong, nonatomic) PPNavigationController *navigationController;

@end

@implementation AppDelegate

- (BOOL)application:(UIApplication *)application didFinishLaunchingWithOptions:(NSDictionary *)launchOptions {
    // Override point for customization after application launch.
    
    self.isAppStart = YES;
    
    window = [[UIWindow alloc] initWithFrame:[[UIScreen mainScreen] bounds]];
    navigationController = [MAppDelegate initialViewControllerWithLaunchOptions:launchOptions];
    
    [window setBackgroundColor:[UIColor blackColor]];
    [window setRootViewController:navigationController];
    [window makeKeyAndVisible];
    
    [[PushManager defaultManager] application:application didFinishLaunchingWithOptions:launchOptions];
    [[PushManager defaultManager] initilaizeWithDelegate:[[PushReceiver alloc] init]];
    
    // MDS App 실행
    self.isMDSCheck = NO;
    [self MDSAppCall];
    
    return YES;
}

- (BOOL)application:(UIApplication *)app openURL:(NSURL *)url options:(NSDictionary<UIApplicationOpenURLOptionsKey,id> *)options {
    NSLog(@"url %@",url);
    NSURLComponents *urlComponents = [NSURLComponents componentsWithURL:url resolvingAgainstBaseURL:NO];
    
    NSLog(@"absoluteString : %@", [urlComponents string]);
    NSLog(@"urlScheme : %@", [urlComponents scheme]);
    NSLog(@"urlHost : %@", [urlComponents host]);
    NSLog(@"query : %@", [urlComponents query]);
    NSLog(@"query item : %@", [urlComponents queryItems]);
    
    if ([[urlComponents scheme] isEqualToString:@"SKIMemberStore"]) {
        if ([[urlComponents host] isEqualToString:@"LOGIN"]) {
            NSMutableDictionary *queryDic = [[NSMutableDictionary alloc] init];
            for (NSURLQueryItem *queryItem in [urlComponents queryItems]) {
                [queryDic setObject:[queryItem value] forKey:[queryItem name]];
            }
            NSLog(@"queryDic : %@", queryDic);
            NSString *appName = [queryDic objectForKey:@"appName"];
            
            if (self.isAppStart) {
                NSMutableDictionary *storeOpenInfoDic = [[NSMutableDictionary alloc] init];
                [storeOpenInfoDic setObject:appName forKey:@"appName"];
                [storeOpenInfoDic setObject:@"LOGIN" forKey:@"type"];
                _setGlobalValue(@"storeOpenInfo", [storeOpenInfoDic jsonString]);
            } else {
                PPWebViewController *current = (PPWebViewController *)navigationController.currentViewCtrl;
                [current callCbfunction:@"fn_storeopen" withObjects:appName, @"LOGIN", nil];
            }
            
        } else if ([[urlComponents host] isEqualToString:@"UPDATE"]) {
            NSMutableDictionary *queryDic = [[NSMutableDictionary alloc] init];
            for (NSURLQueryItem *queryItem in [urlComponents queryItems]) {
                [queryDic setObject:[queryItem value] forKey:[queryItem name]];
            }
            NSLog(@"queryDic : %@", queryDic);
            NSString *appName = [queryDic objectForKey:@"appName"];
            
            if (self.isAppStart) {
                NSMutableDictionary *storeOpenInfoDic = [[NSMutableDictionary alloc] init];
                [storeOpenInfoDic setObject:appName forKey:@"appName"];
                [storeOpenInfoDic setObject:@"UPDATE" forKey:@"type"];
                _setGlobalValue(@"storeOpenInfo", [storeOpenInfoDic jsonString]);
            } else {
                PPWebViewController *current = (PPWebViewController *)navigationController.currentViewCtrl;
                [current callCbfunction:@"fn_storeopen" withObjects:appName, @"UPDATE", nil];
            }
        }
    } else {
        NSMutableString *url_mutable = [[NSMutableString alloc] initWithString:url.absoluteString];
        NSRange range = [url_mutable rangeOfString:@"="];
        
        if (range.location != NSNotFound) {
            self.isMDSCheck = YES;
            [url_mutable deleteCharactersInRange:NSMakeRange(0, range.location + 1)];
            
            [self getData:url_mutable];
        }
    }
    
    return true;
}

- (Boolean)checkMDS {
    NSURL *url = [NSURL URLWithString:@"MDS://"];
    
    if ([[UIApplication sharedApplication] canOpenURL:url] || [url isProxy]) {
        @try {
            UIImagePickerController *picker = [[UIImagePickerController alloc] init];
            picker.delegate = self;
            picker.allowsEditing = YES;
            picker.sourceType = UIImagePickerControllerSourceTypeCamera;
        }
        @catch (NSException *exception) {
            return true;
        }
    } else {
        NSLog(@"checkMDS false");
    }
    
    return false;
}

- (void)MDSAppCall {
    if ([self checkMDS]) {
        NSLog(@"checkMDS : true");
    } else {
        NSLog(@"checkMDS : false");
        NSURL *url = [NSURL URLWithString:@"MDS://CHECK_MDS"];

        if ([[UIApplication sharedApplication] canOpenURL:url]) {
            [[UIApplication sharedApplication] openURL:url
                                               options:@{}
                                     completionHandler:nil];
        } else {
            // SK MDS 미설치 - 설치 링크 이동
//            url = [NSURL URLWithString:@"https://mds.skinnovation.com/downloads"];
            url = [NSURL URLWithString:@"https://svr.funnnew.com:24005/downloads"];
            [[UIApplication sharedApplication] openURL:url
                                               options:@{}
                                     completionHandler:nil];
        }
    }
}

- (void)getData:(NSString *)data_message {
    NSLog(@"getData");
    
    if ([data_message isEqual:@"MDS_NOT_JOINED"]) {
        // SK MDS 설치는 되어있지만 미가입 상태인 경우
        UIAlertController *alertController = [UIAlertController alertControllerWithTitle:@"알림"
                                                                                 message:@"MDS에 가입되어 있지 않습니다.\n앱을 종료 합니다."
                                                                          preferredStyle:UIAlertControllerStyleAlert];
        UIAlertAction *confirmAction = [UIAlertAction actionWithTitle:@"확인"
                                                                style:UIAlertActionStyleDefault
                                                              handler:^(UIAlertAction * _Nonnull action) {
            exit(0);
        }];
        
        [alertController addAction:confirmAction];
        [self.window.rootViewController presentViewController:alertController animated:YES completion:nil];
    } else if ([data_message isEqual:@"MDS_STATE_OFF"]) {
        // SK MDS 의 제어 상태가 OFF 인 경우
        UIAlertController *alertController = [UIAlertController alertControllerWithTitle:@"알림"
                                                                                 message:@"MDS가 OFF상태입니다.\nMDS를 ON후 사용해 주세요."
                                                                          preferredStyle:UIAlertControllerStyleAlert];
        UIAlertAction *confirmAction = [UIAlertAction actionWithTitle:@"확인"
                                                                style:UIAlertActionStyleDefault
                                                              handler:^(UIAlertAction * _Nonnull action) {
            exit(0);
        }];
        
        [alertController addAction:confirmAction];
        [self.window.rootViewController presentViewController:alertController animated:YES completion:nil];
    } else if ([data_message isEqual:@"MDS_STATE_ON"]) {
        // SK MDS 의 제어 상태가 ON 인 경우
        NSLog(@"SK MDS ON");
    }
}

- (void)applicationDidEnterBackground:(UIApplication *)application {
    NSLog(@"applicationDidEnterBackground");
}

- (void)applicationWillEnterForeground:(UIApplication *)application {
    NSLog(@"applicationWillEnterForeground");
}

- (void)applicationDidBecomeActive:(UIApplication *)application {
    NSLog(@"applicationDidBecomeActive");
    self.isAppStart = NO;
    if (self.isMDSCheck) {
        self.isMDSCheck = NO;
    } else {
        [self MDSAppCall];
    }
}

@synthesize window, navigationController;

@end
