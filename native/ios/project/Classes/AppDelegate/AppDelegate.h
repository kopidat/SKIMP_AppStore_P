//
//  AppDelegate.h
//

#import <UIKit/UIKit.h>

@interface AppDelegate : UIResponder <UIApplicationDelegate, UIImagePickerControllerDelegate, UINavigationControllerDelegate>

@property (strong, nonatomic) UIWindow *window;
@property (nonatomic, assign) BOOL isMDSCheck;
@property (assign, nonatomic) BOOL isAppStart;

@end
