declare module 'tdl' {
  export function createClient(config: any): TDLibClient;
  export function configure(options: any): void;

  interface TDLibClient {
    invoke(params: any): Promise<any>;
    on(event: string, handler: Function): void;
    close(): Promise<void>;
    login(phone: any, code?: string): Promise<void>;
  }

  type TDLibRequest = { [key: string]: any } & { '@type': string };

  type TDLibResponse = {
    '@type': string;
    [key: string]: any;
  } | {
    '@type': 'error';
    code: number;
    message: string;
  };
}
